import os

import numpy as np
from langchain_text_splitters import RecursiveCharacterTextSplitter
from pinecone import Pinecone, ServerlessSpec
from sklearn.metrics.pairwise import cosine_similarity

from langchain_pinecone import PineconeVectorStore

from main import embedding_model


def semantic_chunking(text, chunk_size=500, chunk_overlap=50, similarity_threshold=0.85):
    print(chunk_size)
    splitter = RecursiveCharacterTextSplitter(chunk_size=chunk_size, chunk_overlap=chunk_overlap)
    initial_chunks = splitter.split_text(text)
    embeddings = embedding_model.embed_documents(initial_chunks)

    merged_chunks = []
    i = 0
    while i < len(initial_chunks):
        current_chunk = initial_chunks[i]
        current_emb = embeddings[i]

        j = i + 1
        while j < len(initial_chunks):
            sim = cosine_similarity([current_emb], [embeddings[j]])[0][0]
            if sim >= similarity_threshold:
                # Merge similar chunks
                current_chunk += " " + initial_chunks[j]
                # Average embeddings
                current_emb = np.mean([current_emb, embeddings[j]], axis=0)
                j += 1
            else:
                break

        merged_chunks.append(current_chunk)
        i = j

    return merged_chunks


def index_to_pinecone(data):
    pc = Pinecone(api_key=os.getenv("PINECONE_API_KEY"))
    index_name = os.getenv("PINECONE_INDEX_NAME")
    if index_name not in pc.list_indexes().names():
        pc.create_index(
            name=index_name,
            dimension=1536,  # for OpenAI embeddings
            metric="cosine",
            spec=ServerlessSpec(cloud="aws", region="us-east-1")
        )

    # Get index object
    index = pc.Index(name=index_name)
    docs = []

    for post in data["response"]:
        vehicle_detail = post["vehicleDetail"]
        doc_text = f"""
        Title: {post.get('title')}
        Description: {post.get('description')}
        Address: {vehicle_detail.get('address', '')}, {vehicle_detail.get('ward', '')}, {
        vehicle_detail.get('province', '')}
        Price: {post.get('price')}
        Properties: {vehicle_detail.get('properties')}
        Type: {post.get('type')}
        Status: {post.get('status')}
        ManufacturYear: {vehicle_detail.get('manufactureYear')}
        """
        docs.append({
            "id": post["id"],  # keep original ID
            "text": doc_text,  # chunked text
            "metadata": {
                'id': post["id"],
                'type': post['type'],
                'title': post['title'],
                'vehicleDetailId': post['vehicleDetail']['id'],
            }
        })

    PineconeVectorStore.from_texts(
        texts=[doc["text"] for doc in docs],
        embedding=embedding_model,
        metadatas=[doc["metadata"] for doc in docs],
        index_name=index_name,
    )
