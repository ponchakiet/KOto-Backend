import os

from langchain.chains.conversational_retrieval.base import ConversationalRetrievalChain
from langchain_core.messages import BaseMessage, HumanMessage, AIMessage
from langchain_core.prompts import ChatPromptTemplate, MessagesPlaceholder
from langchain_openai import OpenAIEmbeddings, ChatOpenAI
from langchain_pinecone import PineconeVectorStore
from pinecone import Pinecone


def load_vectorstore():
    pc = Pinecone(api_key=os.getenv("PINECONE_API_KEY"))
    index_name = os.getenv("PINECONE_INDEX_NAME")
    index = pc.Index(index_name)
    embedding_model = OpenAIEmbeddings(model="text-embedding-3-small")
    vectorstore = PineconeVectorStore(
        index=index,
        embedding=embedding_model,
        text_key="text"
    )
    return vectorstore


def ask_question(query: str, chat_history=None, k: int = 10):
    vectorstore = load_vectorstore()
    retriever = vectorstore.as_retriever(search_kwargs={"k": k})
    llm = ChatOpenAI(model="gpt-3.5-turbo", temperature=0)

    # Define prompt template
    prompt = ChatPromptTemplate.from_messages([
        ("system",
         "Bạn là một chuyên gia tư vấn ô tô giàu kinh nghiệm tại Việt Nam. "
         "Nhiệm vụ của bạn là: "
         "- Hỗ trợ khách hàng tìm kiếm, lựa chọn và so sánh ô tô phù hợp với nhu cầu và ngân sách "
         "(dựa trên dữ liệu đã cung cấp). "
         "- Tư vấn các thông tin liên quan đến giá xe, thông số, đặc điểm, mục đích sử dụng "
         "và giải pháp tài chính khi khách hàng có nhu cầu mua xe. "
         "Nguyên tắc: "
         "1. Chỉ sử dụng dữ liệu được cung cấp từ ngữ cảnh để trả lời. "
         "Không cần nói rõ nguồn dữ liệu hay nhắc 'dựa vào dữ liệu đã cung cấp'. "
         "Hãy trả lời trực tiếp, tự nhiên, như đang nói chuyện với khách hàng. "
         "2. Nếu dữ liệu không đủ, hãy lịch sự hỏi thêm thông tin cụ thể "
         "(ví dụ: ngân sách, thương hiệu, mẫu xe, kiểu xe, năm sản xuất, số chỗ ngồi hoặc mục đích sử dụng). "
         "3. Nếu sau khi hỏi thêm vẫn không có xe phù hợp trong dữ liệu, "
         "hãy nói rõ ràng và lịch sự rằng hiện tại Koto không thể trả lời. "
         "4. Trả lời tự nhiên, dễ hiểu, như một chuyên gia đang tư vấn trực tiếp cho khách hàng. "
         "5. Chỉ tập trung vào lĩnh vực ô tô và các vấn đề tài chính liên quan đến việc mua ô tô, "
         "không trả lời các nội dung ngoài phạm vi. "
         "6. Nếu người dùng hỏi ngoài phạm vi (ví dụ: lập trình, toán học, nấu ăn...), "
         "hãy lịch sự từ chối và nói: "
         "Xin lỗi, tôi chỉ có thể tư vấn về ô tô và các vấn đề tài chính liên quan đến việc mua ô tô."
         ),

        MessagesPlaceholder(variable_name="history"),

        ("human",
         "Dữ liệu:\n{context}\n\n"
         "Câu hỏi: {question}")
    ])

    formatted_history: list[BaseMessage] = []
    if chat_history:
        for msg in chat_history:
            if isinstance(msg, dict):
                formatted_history.append(HumanMessage(content=msg["human"]))
                formatted_history.append(AIMessage(content=msg["ai"]))
            elif isinstance(msg, BaseMessage):
                formatted_history.append(msg)  # Already valid

    # Set up the chain
    qa_chain = ConversationalRetrievalChain.from_llm(
        llm=llm,
        retriever=retriever,
        combine_docs_chain_kwargs={"prompt": prompt},
        return_source_documents=True
    )
    result = qa_chain.invoke({
        "question": query,
        "chat_history": formatted_history,  # used for context
        "history": formatted_history  # used for prompt rendering
    })
    return {
        "answer": result["answer"],
        "context": [doc.page_content for doc in result["source_documents"]]
    }


def get_query_vector(query: str):
    embedding_model = OpenAIEmbeddings(model="text-embedding-3-small")
    vector = embedding_model.embed_query(query)
    return vector
