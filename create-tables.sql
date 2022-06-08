drop table if exists notebook;
drop table if exists company;
drop table if exists person;
drop table if exists country;

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
    is_ssd boolean,
    has_wifi boolean,
    cpu_speed float,
    production_cost double,
    company_id int not null,
    foreign key(company_id) references company(id)
);

insert into notebook (name, value, is_available, is_ssd, has_wifi,
                      cpu_speed, production_cost, company_id)
values
('ideapad', 9581.20, 'S', true, true, 3000.1, 586.52, 1),
('aspire 3', 1000, 'N', false, true, 2000.1, 500.52, 2),
('aspire 5', 2000, 'S', true, true, 4000.1, 600.52, 2);




create table country (
    id int primary key,
    name varchar(30),
    phone_code int
);

create table person (
    id int primary key,
    name varchar(30),
    country_id int not null,
    foreign key(country_id) references country(id)
);

insert into country(id, name, phone_code) values
(1, 'Brazil', 55),
(2, 'Vietnam', 84),
(3, 'Switzerland', 41),
(4, 'Germany', 49);

insert into person (id, name, country_id) values
(1, 'Leandro', 2),
(2, 'Guilherme', 4),
(3, 'Marcos', 1);


-------------- QUERIES ------------

select * from notebook;

select id, name
from notebook;


-- List company's notebooks
select c.name, n.name
from notebook n join company c on
  n.company_id = c.id;

-- sumValuesGroupedByCompany
select c.id, c.name, sum(n.value) as sum
from notebook n join company c on
    n.company_id = c.id
group by c.id, c.name;