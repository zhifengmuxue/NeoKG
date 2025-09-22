CREATE EXTENSION IF NOT EXISTS vector;
CREATE TABLE document
(
    id         BIGSERIAL PRIMARY KEY, -- 对应 Document.id
    title      TEXT NOT NULL,         -- Document.title
    content    TEXT,                  -- Document.content
    type       varchar(50),          -- Document.type
    vec        vector(1024),          -- Document embedding 向量
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- 向量索引
CREATE INDEX idx_document_vec ON document USING ivfflat (vec vector_l2_ops);


CREATE TABLE keyword
(
    id          BIGSERIAL PRIMARY KEY,
    name        TEXT NOT NULL,                       -- Keyword.tag
    description TEXT,                                -- Keyword.description
    alias       TEXT[],                              -- Keyword.alias
    vec      vector(1024),                        -- Keyword embedding
    created_at  TIMESTAMP DEFAULT NOW(),
    updated_at  TIMESTAMP DEFAULT NOW()
);

-- 向量索引
CREATE INDEX idx_keyword_vec ON keyword USING ivfflat (vec vector_l2_ops);

-- alias 数组索引（可选）
CREATE INDEX idx_keyword_alias ON keyword USING gin (alias);

CREATE TABLE document_ref
(
    id          BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL REFERENCES document (id) ON DELETE CASCADE, -- 被引用文档
    keyword_id BIGINT NOT NULL REFERENCES keyword (id) ON DELETE CASCADE,
    ref_index   BIGINT      DEFAULT 0,                                      -- 对应 Keyword.ref.index
    created_at  TIMESTAMP DEFAULT NOW(),
    updated_at  TIMESTAMP DEFAULT NOW()
);

-- 索引
CREATE INDEX idx_document_ref_document ON document_ref (document_id);

ALTER TABLE document
ALTER COLUMN id SET DEFAULT nextval('document_id_seq');

-- 用户表
CREATE TABLE sys_user (
id BIGSERIAL PRIMARY KEY,
username VARCHAR(100) NOT NULL UNIQUE,
password VARCHAR(255) NOT NULL,
is_enable BOOLEAN DEFAULT TRUE,

created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 图表
CREATE TABLE graph (
id BIGSERIAL PRIMARY KEY,
user_id BIGINT NOT NULL REFERENCES sys_user(id) ON DELETE CASCADE,
name VARCHAR(200) NOT NULL,
description TEXT,

created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 索引（可选，提升查询效率）
CREATE INDEX idx_graph_user_id ON graph(user_id);
