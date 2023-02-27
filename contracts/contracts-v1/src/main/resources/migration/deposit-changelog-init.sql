create table DEPOSITS(
        output_index int4 not null,
        transaction_id varchar(144) not null,
        owner varchar(255) not null,
        treasury varchar(255) not null,
        amount int4 not null,
        currency varchar(255) not null,
        account_id varchar(255) not null,
        primary key (output_index, transaction_id)
);