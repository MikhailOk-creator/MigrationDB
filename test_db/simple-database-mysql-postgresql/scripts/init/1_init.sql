create table if not exists band_t (
	id int4 NOT NULL AUTO_INCREMENT primary key,
	name_of_band varchar(255) NOT NULL,
	start_year int NOT NULL,
	end_year int NULL
);

create table if not exists musician_t (
	id int NOT NULL AUTO_INCREMENT primary key,
	name_of_musician varchar(255) NOT NULL,
	country varchar(255) NOT NULL,
	full_real_name varchar(255) NULL,
	birth_year int NOT NULL,
	death_year varchar(255) NULL
);

CREATE TABLE band_and_musician_t (
	band_id int NOT NULL,
	musician_id int NOT NULL,
	musical_instrument varchar(255) NOT null,
	primary key (band_id, musician_id),
	foreign key (band_id) references band_t(id),
	foreign key (musician_id) references musician_t(id)
);