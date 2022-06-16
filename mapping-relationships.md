# How to map relationships?

I think I'll use something very similiar to Hibernate:
https://docs.jboss.org/hibernate/jpa/2.1/api/javax/persistence/OneToOne.html


@Id will be needed when there's a "many" relationship involved: OneToMany and ManyToMany

When you have a many to many relationship between tables,
you'll need to have a join table in the RDBMS, but you don't
necessarily need a join Class in Java, because you only need it
if there's some information that belongs to the join table.

And if there's a join Class, then you won't have a many to many
relationship, but two one to many relationships.
