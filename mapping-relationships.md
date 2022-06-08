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