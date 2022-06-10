# How to map relationships?

Now it's generating lists for every class, which is already
pretty good, but the user has to manage the Java relationships manually.

I need to map two types of relationships:
  1. One to One
  2. One to Many / Many to One

I think I'll use something very similiar to Hibernate:
https://docs.jboss.org/hibernate/jpa/2.1/api/javax/persistence/OneToOne.html

But I need less information, and in order to use it the user
must tell which columns form the primary key using @Id annotation.

@Id will be needed when there's a "many" relationship involved: OneToMany, ManyToOne, ManyToMany

When you have a many to many relationship between tables,
you'll need to have a join table in the RDBMS, but you don't
necessarily need a join Class in Java, because you only need it
if there's some information that belongs to the join table.

And if there's a join Class, then you won't have a many to many
relationship, but two one to many relationships.