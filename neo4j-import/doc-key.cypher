// docId 唯一约束
CREATE CONSTRAINT document_docId_unique IF NOT EXISTS
FOR (d:DocumentNode) REQUIRE d.docId IS UNIQUE;