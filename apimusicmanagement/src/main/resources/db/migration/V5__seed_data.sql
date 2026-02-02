-- =========================
-- V5 - Seed inicial (Artistas, Generos, Albuns e vinculos Album<->Genero)
-- Idempotente: evita duplicidade via NOT EXISTS / ON CONFLICT
-- =========================

-- -------------------------
-- ARTISTAS
-- -------------------------
INSERT INTO artista (nome, tipo)
SELECT 'Serj Tankian', 'SOLO'
WHERE NOT EXISTS (SELECT 1 FROM artista WHERE nome = 'Serj Tankian');

INSERT INTO artista (nome, tipo)
SELECT 'Mike Shinoda', 'SOLO'
WHERE NOT EXISTS (SELECT 1 FROM artista WHERE nome = 'Mike Shinoda');

INSERT INTO artista (nome, tipo)
SELECT 'Michel Teló', 'SOLO'
WHERE NOT EXISTS (SELECT 1 FROM artista WHERE nome = 'Michel Teló');

INSERT INTO artista (nome, tipo)
SELECT 'Guns N'' Roses', 'BANDA'
WHERE NOT EXISTS (SELECT 1 FROM artista WHERE nome = 'Guns N'' Roses');

-- -------------------------
-- GENEROS
-- -------------------------
-- Aproveita unique constraint uk_genero_nome (V2) para idempotência
INSERT INTO genero (nome)
VALUES
  ('Rock'),
  ('Hard Rock'),
  ('Alternative Rock'),
  ('Hip Hop'),
  ('Sertanejo')
ON CONFLICT (nome) DO NOTHING;

-- -------------------------
-- ALBUNS
-- -------------------------
-- Serj Tankian
INSERT INTO album (nome, artista_id)
SELECT 'Harakiri', a.id
FROM artista a
WHERE a.nome = 'Serj Tankian'
  AND NOT EXISTS (SELECT 1 FROM album al WHERE al.nome = 'Harakiri' AND al.artista_id = a.id);

INSERT INTO album (nome, artista_id)
SELECT 'Black Blooms', a.id
FROM artista a
WHERE a.nome = 'Serj Tankian'
  AND NOT EXISTS (SELECT 1 FROM album al WHERE al.nome = 'Black Blooms' AND al.artista_id = a.id);

INSERT INTO album (nome, artista_id)
SELECT 'The Rough Dog', a.id
FROM artista a
WHERE a.nome = 'Serj Tankian'
  AND NOT EXISTS (SELECT 1 FROM album al WHERE al.nome = 'The Rough Dog' AND al.artista_id = a.id);

-- Mike Shinoda
INSERT INTO album (nome, artista_id)
SELECT 'The Rising Tied', a.id
FROM artista a
WHERE a.nome = 'Mike Shinoda'
  AND NOT EXISTS (SELECT 1 FROM album al WHERE al.nome = 'The Rising Tied' AND al.artista_id = a.id);

INSERT INTO album (nome, artista_id)
SELECT 'Post Traumatic', a.id
FROM artista a
WHERE a.nome = 'Mike Shinoda'
  AND NOT EXISTS (SELECT 1 FROM album al WHERE al.nome = 'Post Traumatic' AND al.artista_id = a.id);

INSERT INTO album (nome, artista_id)
SELECT 'Post Traumatic EP', a.id
FROM artista a
WHERE a.nome = 'Mike Shinoda'
  AND NOT EXISTS (SELECT 1 FROM album al WHERE al.nome = 'Post Traumatic EP' AND al.artista_id = a.id);

INSERT INTO album (nome, artista_id)
SELECT 'Where''d You Go', a.id
FROM artista a
WHERE a.nome = 'Mike Shinoda'
  AND NOT EXISTS (SELECT 1 FROM album al WHERE al.nome = 'Where''d You Go' AND al.artista_id = a.id);

-- Michel Teló
INSERT INTO album (nome, artista_id)
SELECT 'Bem Sertanejo', a.id
FROM artista a
WHERE a.nome = 'Michel Teló'
  AND NOT EXISTS (SELECT 1 FROM album al WHERE al.nome = 'Bem Sertanejo' AND al.artista_id = a.id);

INSERT INTO album (nome, artista_id)
SELECT 'Bem Sertanejo - O Show (Ao Vivo)', a.id
FROM artista a
WHERE a.nome = 'Michel Teló'
  AND NOT EXISTS (SELECT 1 FROM album al WHERE al.nome = 'Bem Sertanejo - O Show (Ao Vivo)' AND al.artista_id = a.id);

INSERT INTO album (nome, artista_id)
SELECT 'Bem Sertanejo - (1ª Temporada) - EP', a.id
FROM artista a
WHERE a.nome = 'Michel Teló'
  AND NOT EXISTS (SELECT 1 FROM album al WHERE al.nome = 'Bem Sertanejo - (1ª Temporada) - EP' AND al.artista_id = a.id);

-- Guns N' Roses
INSERT INTO album (nome, artista_id)
SELECT 'Use Your Illusion I', a.id
FROM artista a
WHERE a.nome = 'Guns N'' Roses'
  AND NOT EXISTS (SELECT 1 FROM album al WHERE al.nome = 'Use Your Illusion I' AND al.artista_id = a.id);

INSERT INTO album (nome, artista_id)
SELECT 'Use Your Illusion II', a.id
FROM artista a
WHERE a.nome = 'Guns N'' Roses'
  AND NOT EXISTS (SELECT 1 FROM album al WHERE al.nome = 'Use Your Illusion II' AND al.artista_id = a.id);

INSERT INTO album (nome, artista_id)
SELECT 'Greatest Hits', a.id
FROM artista a
WHERE a.nome = 'Guns N'' Roses'
  AND NOT EXISTS (SELECT 1 FROM album al WHERE al.nome = 'Greatest Hits' AND al.artista_id = a.id);

-- -------------------------
-- VINCULOS ALBUM <-> GENERO (album_genero)
-- -------------------------
-- Serj Tankian -> Alternative Rock
INSERT INTO album_genero (album_id, genero_id)
SELECT al.id, g.id
FROM album al
JOIN artista a ON a.id = al.artista_id
JOIN genero g ON g.nome = 'Alternative Rock'
WHERE a.nome = 'Serj Tankian'
  AND al.nome IN ('Harakiri', 'Black Blooms', 'The Rough Dog')
  AND NOT EXISTS (
      SELECT 1 FROM album_genero ag
      WHERE ag.album_id = al.id AND ag.genero_id = g.id
  );

-- Mike Shinoda -> Hip Hop + Alternative Rock
INSERT INTO album_genero (album_id, genero_id)
SELECT al.id, g.id
FROM album al
JOIN artista a ON a.id = al.artista_id
JOIN genero g ON g.nome = 'Hip Hop'
WHERE a.nome = 'Mike Shinoda'
  AND al.nome IN ('The Rising Tied', 'Post Traumatic', 'Post Traumatic EP', 'Where''d You Go')
  AND NOT EXISTS (
      SELECT 1 FROM album_genero ag
      WHERE ag.album_id = al.id AND ag.genero_id = g.id
  );

INSERT INTO album_genero (album_id, genero_id)
SELECT al.id, g.id
FROM album al
JOIN artista a ON a.id = al.artista_id
JOIN genero g ON g.nome = 'Alternative Rock'
WHERE a.nome = 'Mike Shinoda'
  AND al.nome IN ('The Rising Tied', 'Post Traumatic', 'Post Traumatic EP', 'Where''d You Go')
  AND NOT EXISTS (
      SELECT 1 FROM album_genero ag
      WHERE ag.album_id = al.id AND ag.genero_id = g.id
  );

-- Michel Teló -> Sertanejo
INSERT INTO album_genero (album_id, genero_id)
SELECT al.id, g.id
FROM album al
JOIN artista a ON a.id = al.artista_id
JOIN genero g ON g.nome = 'Sertanejo'
WHERE a.nome = 'Michel Teló'
  AND al.nome IN ('Bem Sertanejo', 'Bem Sertanejo - O Show (Ao Vivo)', 'Bem Sertanejo - (1ª Temporada) - EP')
  AND NOT EXISTS (
      SELECT 1 FROM album_genero ag
      WHERE ag.album_id = al.id AND ag.genero_id = g.id
  );

-- Guns N' Roses -> Rock + Hard Rock
INSERT INTO album_genero (album_id, genero_id)
SELECT al.id, g.id
FROM album al
JOIN artista a ON a.id = al.artista_id
JOIN genero g ON g.nome = 'Rock'
WHERE a.nome = 'Guns N'' Roses'
  AND al.nome IN ('Use Your Illusion I', 'Use Your Illusion II', 'Greatest Hits')
  AND NOT EXISTS (
      SELECT 1 FROM album_genero ag
      WHERE ag.album_id = al.id AND ag.genero_id = g.id
  );

INSERT INTO album_genero (album_id, genero_id)
SELECT al.id, g.id
FROM album al
JOIN artista a ON a.id = al.artista_id
JOIN genero g ON g.nome = 'Hard Rock'
WHERE a.nome = 'Guns N'' Roses'
  AND al.nome IN ('Use Your Illusion I', 'Use Your Illusion II', 'Greatest Hits')
  AND NOT EXISTS (
      SELECT 1 FROM album_genero ag
      WHERE ag.album_id = al.id AND ag.genero_id = g.id
  );
