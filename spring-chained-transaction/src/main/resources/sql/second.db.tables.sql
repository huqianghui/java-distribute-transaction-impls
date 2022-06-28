CREATE TABLE second_table(
    id int NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary Key',
    name VARCHAR(255) COMMENT 'name',
    create_time DATETIME COMMENT 'Create Time',
    update_time DATETIME COMMENT 'Update Time'
) DEFAULT CHARSET UTF8 COMMENT 'second_table';