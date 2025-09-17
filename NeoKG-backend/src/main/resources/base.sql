CREATE EXTENSION IF NOT EXISTS vector;
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

CREATE TABLE document_ref (
id BIGSERIAL PRIMARY KEY,
document_id BIGINT NOT NULL REFERENCES document(id) ON DELETE CASCADE,   -- 被引用文档
ref_index BIGINT DEFAULT 0,               -- 对应 Keyword.ref.index
created_at TIMESTAMPTZ DEFAULT NOW(),
updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 索引
CREATE INDEX idx_document_ref_document ON document_ref(document_id);

CREATE TABLE keyword (
 id BIGSERIAL PRIMARY KEY,
 tag TEXT NOT NULL,                              -- Keyword.tag
 description TEXT,                               -- Keyword.description
 alias TEXT[],                                   -- Keyword.alias
 vector vector(1536),                               -- Keyword embedding
 ref_id BIGINT REFERENCES document_ref(id),      -- 对应 DocumentRef
 created_at TIMESTAMPTZ DEFAULT NOW(),
 updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 向量索引
CREATE INDEX idx_keyword_vec ON keyword USING ivfflat (vector vector_l2_ops);

-- alias 数组索引（可选）
CREATE INDEX idx_keyword_alias ON keyword USING gin (alias);

-- 插入 document 表的模拟数据（修复语法错误）
INSERT INTO document (title, content, keywords, vector)
VALUES
    ('Document 1', 'This is the content of document 1', '[{"tag": "AI", "description": "Artificial Intelligence"}]', ARRAY[0.1, 0.2, 0.3] || ARRAY_FILL(0.0::float4, ARRAY[1533])),
    ('Document 2', 'This is the content of document 2', '[{"tag": "ML", "description": "Machine Learning"}]', ARRAY[0.4, 0.5, 0.6] || ARRAY_FILL(0.0::float4, ARRAY[1533])),
    ('Document 3', 'This is the content of document 3', '[{"tag": "NLP", "description": "Natural Language Processing"}]', ARRAY[0.7, 0.8, 0.9] || ARRAY_FILL(0.0::float4, ARRAY[1533]));

-- 插入 document_ref 表的模拟数据
INSERT INTO document_ref (document_id, ref_index)
VALUES
    (1, 1),
    (1, 2),
    (2, 1);

-- 插入 keyword 表的模拟数据（修复向量维度）
INSERT INTO keyword (tag, description, alias, vector, ref_id)
VALUES
    ( 'AI', 'Artificial Intelligence', '{"Artificial Intelligence", "AI"}', ARRAY[0.1, 0.2, 0.3] || ARRAY_FILL(0.0::float4, ARRAY[1533]), 1),
    ('Deep Learning', 'A subset of Machine Learning', '{"Deep Learning", "DL"}', ARRAY[0.2, 0.3, 0.4] || ARRAY_FILL(0.0::float4, ARRAY[1533]), 2),
    ('ML', 'Machine Learning', '{"Machine Learning", "ML"}', ARRAY[0.4, 0.5, 0.6] || ARRAY_FILL(0.0::float4, ARRAY[1533]), NULL),
    ('NLP', 'Natural Language Processing', '{"Natural Language Processing", "NLP"}', ARRAY[0.7, 0.8, 0.9] || ARRAY_FILL(0.0::float4, ARRAY[1533]), NULL);

CREATE TABLE document_keyword (
document_id BIGINT NOT NULL REFERENCES document(id) ON DELETE CASCADE,
keyword_id BIGINT NOT NULL REFERENCES keyword(id) ON DELETE CASCADE,
PRIMARY KEY (document_id, keyword_id)
);

-- 索引
CREATE INDEX idx_document_keyword_document ON document_keyword(document_id);
CREATE INDEX idx_document_keyword_keyword ON document_keyword(keyword_id);
