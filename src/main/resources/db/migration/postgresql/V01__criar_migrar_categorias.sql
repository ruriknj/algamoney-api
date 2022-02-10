CREATE SEQUENCE categoria_seq;

CREATE TABLE categoria (
	codigo BIGINT PRIMARY KEY DEFAULT NEXTVAL ('categoria_seq'),
	nome VARCHAR(50) NOT NULL
) ;

INSERT INTO categoria (nome) values ('Lazer');
INSERT INTO categoria (nome) values ('Alimentação');
INSERT INTO categoria (nome) values ('Supermercado');
INSERT INTO categoria (nome) values ('Farmácia');
INSERT INTO categoria (nome) values ('Outros');