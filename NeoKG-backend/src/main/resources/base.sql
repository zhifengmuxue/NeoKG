CREATE TABLE document (
  id BIGSERIAL PRIMARY KEY,                     -- 对应 Document.id
  title TEXT NOT NULL,                          -- Document.title
  content TEXT,                                 -- Document.content
  keywords JSONB,                               -- Document.keywords 存 Keyword 列表
  vector vector(1536),                          -- Document embedding 向量
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 向量索引
CREATE INDEX idx_document_vec ON document USING ivfflat (vector vector_l2_ops);

-- JSONB 索引（keywords）
CREATE INDEX idx_document_keywords ON document USING gin (keywords);

CREATE TABLE keyword (
 id BIGSERIAL PRIMARY KEY,
 document_id BIGINT NOT NULL REFERENCES document(id) ON DELETE CASCADE, -- 所属 Document
 tag TEXT NOT NULL,                              -- Keyword.tag
 description TEXT,                               -- Keyword.description
 alias TEXT[],                                   -- Keyword.alias
 vector vector(1536),                               -- Keyword embedding
 ref_id BIGINT REFERENCES document_ref(id),      -- 对应 DocumentRef
 created_at TIMESTAMPTZ DEFAULT NOW(),
 updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 向量索引
CREATE INDEX idx_keyword_vec ON keyword USING ivfflat (vec vector_l2_ops);

-- alias 数组索引（可选）
CREATE INDEX idx_keyword_alias ON keyword USING gin (alias);

CREATE TABLE document_ref (
      id BIGSERIAL PRIMARY KEY,
      document_id BIGINT NOT NULL REFERENCES document(id) ON DELETE CASCADE,   -- 被引用文档
      ref_index BIGINT DEFAULT 0,               -- 对应 Keyword.ref.index
      created_at TIMESTAMPTZ DEFAULT NOW(),
      updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 索引
CREATE INDEX idx_document_ref_document ON document_ref(document_id);
CREATE INDEX idx_document_ref_target ON document_ref(ref_document_id);

-- 插入 Document
INSERT INTO document (title, content, keywords, vector)
VALUES (
'示例文档',
'这是文档内容示例。',
'[{"tag":"AI","description":"人工智能","alias":["AI","人工智能"],"ref":{"documentId":"1","index":0}}]'::jsonb,
'[0.1,0.2,0.3,...,0.1536]'::vector
);

-- 插入 DocumentRef
INSERT INTO document_ref (document_id, ref_document_id, ref_index)
VALUES (1, 2, 0);

-- 插入 Keyword
INSERT INTO keyword (document_id, tag, description, alias, vector, ref_id)
VALUES (
1,
'AI',
'人工智能',
ARRAY['AI','人工智能'],
'[0.1,0.2,0.3,...,0.1536]'::vector,
1
);

