from typing import List, Optional

from pydantic import BaseModel


class QueryResponse(BaseModel):
    answer: str
    context: Optional[list] = None
