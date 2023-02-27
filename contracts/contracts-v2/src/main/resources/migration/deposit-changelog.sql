create table DEPOSITS(
        output_index int4 not null,
        transaction_id varchar(144) not null,
        owner varchar(255) not null,
        treasury timestamp not null,
        amount int4 not null,
        currency varchar(255) not null,
        account_id varchar(255) not null,
        current_owner varchar(255),
        primary key (output_index, transaction_id)
);