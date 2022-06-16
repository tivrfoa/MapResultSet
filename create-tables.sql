drop table if exists notebook;
drop table if exists company;
drop table if exists Phone;
drop table if exists person_address;
drop table if exists person;
drop table if exists address;
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
    phone_code int,
    someBigNumber BigInt,
    evenBigger decimal(38, 0)
);

insert into country values
(1, 'Brazil', 55, 11111111111112, 1111111111111211111111111112),
(2, 'Vietnam', 84, 11111111111113, 1111111111111311111111111113),
(3, 'Switzerland', 41, 11111111111114, 1111111111111411111111111114),
(4, 'Germany', 49, 11111111111115, 1111111111111511111111111115);

create table person (
    id int primary key,
    name varchar(30),
    born_timestamp timestamp,
    wakeup_time time,
    country_id int not null,
    foreign key(country_id) references country(id)
);


insert into person values
(1, 'Leandro', current_timestamp, current_time + 3, 2),
(2, 'Guilherme', '1989-07-03 08:09:10', current_time + 1, 4),
(3, 'Marcos', '1985-03-04 05:06:07', current_time + 2, 1);

create table Phone (
    id int primary key,
    number int,
    person_id int,
    foreign key(person_id) references person(id)
);

insert into Phone values
(1, 1111, 1),
(2, 1112, 1),
(3, 2222, 2),
(4, 3333, 3);

create table address (
    id int primary key,
    street varchar(30)
);

insert into address values
(1, 'Halong Bay'),
(2, 'Xie Xie'),
(3, 'Arigato');

create table person_address (
    person_id int,
    address_id int,
    foreign key (person_id) references person(id),
    foreign key (address_id) references address(id)
);

insert into person_address values
(1, 1),
(2, 2),
(3, 3);


-------------- QUERIES ------------

select * from notebook;
select * from person;
select * from country;

select id, name from notebook;


-- List company's notebooks
select c.name, n.name
from notebook n join company c on
  n.company_id = c.id;

-- sumValuesGroupedByCompany
select c.id, c.name, sum(n.value) as sum
from notebook n join company c on
    n.company_id = c.id
group by c.id, c.name;