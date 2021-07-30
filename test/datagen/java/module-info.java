module opencsvDemo {
    requires opencsv;
    requires java.sql;  // That this is needed was not evident; I was helped by StackOverflow to fix it.

    opens demo;
}