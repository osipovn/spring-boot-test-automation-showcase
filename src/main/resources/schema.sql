create table if not exists notes (
  id bigserial primary key,
  body text not null
);
