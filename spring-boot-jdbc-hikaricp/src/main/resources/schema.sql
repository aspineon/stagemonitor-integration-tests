drop table note if exists;
create table note (id bigint generated by default as identity (start with 1), body varchar(255), title varchar(255), primary key (id));