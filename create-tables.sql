drop table if exists notebook;
drop table if exists company;
drop table if exists person;

create table company (
    id int primary key,
    name varchar(30)
);

insert into company values
(1, 'Lenovo'),
(2, 'Acer'),
(3, 'Dell');

create table notebook (
    id int primary key auto_increment,
    name varchar(30),
    value decimal(7, 2),
    release_date date default(current_date),
    is_available char(1),
    company_id int not null,
    foreign key(company_id) references company(id)
);

insert into notebook (name, value, is_available, company_id) values
('ideapad', 9581.20, 'S', 1),
('aspire 5', 2000, 'N', 2);





-------------- QUERIES ------------

select * from notebook;

select id, name
from notebook;


-- List company's notebooks
select c.name, n.name
from notebook n join company c on
  n.company_id = c.id;