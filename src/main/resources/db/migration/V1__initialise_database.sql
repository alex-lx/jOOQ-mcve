DROP SCHEMA IF EXISTS mcve CASCADE;

CREATE SCHEMA mcve;

CREATE TABLE mcve.test (
  id    INT NOT NULL AUTO_INCREMENT,
  value INT,
  
  CONSTRAINT pk_test PRIMARY KEY (id) 
);

create table mcve.task (
    group_id  int,
    member_id int,
    constraint pk_task primary key (group_id, member_id)
);

create table mcve.completed_task (
    group_id int,
    member_id int,
    constraint pk_completed_task primary key (group_id, member_id)
);