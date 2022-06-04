drop table notebook;
drop table company;
drop table person;

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
    company_id int not null,
    foreign key(company_id) references company(id)
);

insert into notebook (name, company_id) values
('ideapad', 1),
('aspire 5', 2);

update notebook
set value = 1234.56
where id = 2;




-------------- QUERIES ------------

select id, name
from notebook;


-- List company's notebooks
select c.name, n.name
from notebook n join company c on
  n.company_id = c.id;