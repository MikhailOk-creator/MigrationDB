INSERT INTO band_t (name_of_band,start_year,end_year) VALUES
	 ('Rammstein',1994,null),
	 ('Daft Punk',1993,2021);

INSERT INTO public.musician_t (name_of_musician,country,full_real_name,birth_year,death_year) VALUES
	 ('Till Lindeman','Germany',NULL,1963,NULL),
	 ('Richard Kruspe','Germany','Sven Kruspe',1967,NULL),
	 ('Paul Landers','Germany','Heiko Paul Hiersche',1964,NULL),
	 ('Oliver Riedel','Germany',NULL,1971,NULL),
	 ('Christoph Schneider','Germany',NULL,1966,NULL),
	 ('Christian Flake Lorenz','Germany','Christian Lorenz',1966,NULL),
	 ('Thomas Bangalter','France',NULL,1975,NULL),
	 ('Guy-Manuel','France','Guy-Manuel de Homem-Christo',1974,NULL);

INSERT INTO public.band_and_musician_t (band_id,musician_id,musical_instrument) VALUES
	 (1,1,'vocal'),
	 (1,2,'guitar'),
	 (1,3,'guitar'),
	 (1,4,'bass'),
	 (1,5,'drums'),
	 (1,6,'synthesizers'),
	 (2,7,'synthesizers'),
	 (2,8,'synthesizers');
