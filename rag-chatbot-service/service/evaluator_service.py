import os
from datetime import datetime

import openai
import pandas as pd
import asyncio

from dotenv import load_dotenv
from langchain_openai import ChatOpenAI
from ragas import SingleTurnSample
from ragas.embeddings import OpenAIEmbeddings
from ragas.llms import LangchainLLMWrapper
from ragas.metrics import Faithfulness, AnswerRelevancy, ContextRecall, ContextPrecision

from service.rag_qa import ask_question

load_dotenv()
openai_client = openai.OpenAI(api_key=os.getenv("OPENAI_API_KEY"))

evaluator_llm = LangchainLLMWrapper(ChatOpenAI(model="gpt-3.5-turbo"))


class FixedOpenAIEmbeddings(OpenAIEmbeddings):
    def embed_query(self, text: str):
        return self.embed_text(text)

    def embed_document(self, texts):
        """Embed multiple documents/texts"""
        if isinstance(texts, str):
            texts = [texts]
        return [self.embed_text(text) for text in texts]


# Khởi tạo embeddings cho RAGAS
evaluator_embeddings = FixedOpenAIEmbeddings(
    model="text-embedding-3-small",
    client=openai_client
)

faithfulness_metric = Faithfulness(llm=evaluator_llm)

metrics_instances = {
    "faithfulness": Faithfulness(llm=evaluator_llm),
    "answer_relevancy": AnswerRelevancy(llm=evaluator_llm, embeddings=evaluator_embeddings),
    "context_recall": ContextRecall(llm=evaluator_llm),
    "context_precision": ContextPrecision(llm=evaluator_llm),
}


async def evaluate_sample_multi_metric(sample):
    rag_result = ask_question(sample["question"], sample["history"])

    rag_answer = rag_result.get("answer", "")
    rag_contexts = rag_result.get("context", [])

    no_info_case = sample.get("no_info_case", False)

    result = {
        "question": sample["question"],
        "reference": sample.get("expected_answer", ""),
        "rag_answer": rag_answer,
        "context": " | ".join(rag_contexts),
    }

    if no_info_case:
        # Custom evaluation
        if any(kw in rag_answer.lower() for kw in ["không có", "không tìm thấy", "chưa có thông tin"]):
            result["faithfulness_score"] = 1.0
            result["answer_relevancy_score"] = 1.0
        else:
            result["faithfulness_score"] = 0.0
            result["answer_relevancy_score"] = 0.0
        result["context_recall_score"] = None
        result["context_precision_score"] = None
    else:
        # Evaluate with RAGAS normally
        single_sample = SingleTurnSample(
            user_input=sample["question"],
            response=rag_answer,
            retrieved_contexts=rag_contexts,
            reference=sample.get("expected_answer", "")
        )

        for name, metric in metrics_instances.items():
            try:
                score = await metric.single_turn_ascore(sample=single_sample)
                result[name + "_score"] = score
            except Exception as e:
                print(f"Error calculating {name} for question '{sample['question']}': {e}")
                result[name + "_score"] = None

    return result


async def evaluate_batch(samples):
    tasks = [evaluate_sample_multi_metric(sample) for sample in samples]
    return await asyncio.gather(*tasks)


sample_list = [
    {
        "question": "Xu hướng thị trường ô tô hiện tại ở Việt Nam",
        "expected_answer": "Thị trường ô tô Việt Nam đang có nhiều biến động, người mua ngày càng quan tâm đến xe tiết kiệm nhiên liệu, xe hybrid, xe điện và các mẫu xe có nhiều trang bị an toàn. Khi lựa chọn xe, bạn nên cân nhắc nhu cầu sử dụng, ngân sách và chi phí vận hành.",
        "history": [],
    },
    {
        "question": "Tui nên mua xe ở khu vực nào ở TP.HCM",
        "expected_answer": "Nếu bạn đang tìm mua ô tô tại TP.HCM, bạn có thể ưu tiên các khu vực có nhiều showroom và đại lý như Bình Thạnh, Gò Vấp, Quận 7, Thủ Đức hoặc Tân Bình. Nếu bạn ưu tiên nhiều lựa chọn và dễ so sánh giá, nên tìm các khu vực tập trung nhiều đại lý.",
        "history": [],
    },
    {
        "question": "Tui cần vay khoảng 500 triệu để mua xe. Có những lựa chọn nào?",
        "expected_answer": "Bạn có thể cân nhắc các khoản vay mua ô tô từ ngân hàng hoặc công ty tài chính. Khi lựa chọn khoản vay, nên xem xét lãi suất, tỷ lệ vay trên giá trị xe, thời hạn vay, số tiền trả hàng tháng và các khoản phí liên quan. Nếu bạn cho mình biết giá chiếc xe và số tiền trả trước, mình có thể giúp bạn ước tính khoản vay phù hợp.",
        "history": [],
    },
    {
        "question": "Tui muốn mua xe khoảng 1 tỷ ở TP.HCM có không?",
        "expected_answer": "Dựa trên dữ liệu hiện có, tôi chưa tìm thấy mẫu xe phù hợp hoàn toàn với yêu cầu này. Bạn có thể cho mình biết thêm loại xe mong muốn như sedan, SUV, hatchback hoặc bán tải để mình tìm kiếm chính xác hơn.",
        "no_info_case": True,
        "history": [],
    },
    {
        "question": "Giới thiệu cho tui các xe ở Gò Vấp giá khoảng 500 triệu",
        "expected_answer": "Hiện tại, dữ liệu tìm kiếm chưa có mẫu xe phù hợp hoàn toàn với ngân sách khoảng 500 triệu tại Gò Vấp. Bạn có thể mở rộng phạm vi sang các khu vực lân cận hoặc tăng khoảng ngân sách để có thêm lựa chọn.",
        "history": [],
    },
    {
        "question": "Tui muốn mua xe cũ ở Bình Thạnh",
        "expected_answer": "Bạn có thể tham khảo các mẫu xe đã qua sử dụng đang được đăng bán tại Bình Thạnh. Khi mua xe cũ, nên kiểm tra kỹ năm sản xuất, số km đã đi, tình trạng động cơ, hộp số, lịch sử bảo dưỡng và giấy tờ xe.",
        "history": [],
    },
    {
        "question": "Hiện tại mua Toyota Corolla Cross khoảng 800 triệu là mắc hay rẻ?",
        "expected_answer": "Để đánh giá mức giá Toyota Corolla Cross khoảng 800 triệu là cao hay thấp, cần xem xét năm sản xuất, phiên bản, tình trạng xe, số km đã đi và các trang bị đi kèm. Nếu là xe đã qua sử dụng, những yếu tố này ảnh hưởng đáng kể đến giá bán.",
        "history": [],
    },
    {
        "question": "Khoảng 1 tỷ",
        "expected_answer": "Với ngân sách khoảng 1 tỷ, bạn có thể cân nhắc nhiều mẫu sedan, SUV hoặc crossover tùy nhu cầu. Nếu bạn ưu tiên gia đình, có thể tập trung vào SUV/crossover; nếu ưu tiên đi phố và tiết kiệm nhiên liệu, sedan hoặc hatchback có thể phù hợp hơn.",
        "history": [
            {
                "human": "Tui muốn mua xe ở Bình Thạnh",
                "ai": "Bạn có thể cho mình biết thêm ngân sách dự kiến và loại xe bạn muốn mua để mình tìm kiếm chính xác hơn."
            }
        ]
    },
    {
        "question": "Cho tui một chiến lược mua xe lâu dài",
        "expected_answer": "Nếu sử dụng xe lâu dài, bạn nên ưu tiên những mẫu xe có độ bền tốt, chi phí bảo dưỡng hợp lý, mức tiêu hao nhiên liệu thấp và phụ tùng dễ tìm. Ngoài giá mua ban đầu, cũng nên tính đến chi phí bảo hiểm, bảo dưỡng, nhiên liệu và khả năng giữ giá của xe.",
        "history": [],
    },
]

if __name__ == "__main__":
    all_results = asyncio.run(evaluate_batch(sample_list))
    df = pd.DataFrame(all_results)

    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    filename = f"ragas_metrics_report_{timestamp}.csv"

    df.to_csv(f"../evaluator_reports/{filename}", index=False, encoding="utf-8-sig")
