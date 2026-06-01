update app_users
set password_hash = 'pbkdf2-sha256$120000$YWRtaW4tc2FsdC0yMDI2$jpFxy5GN5EbQJManFazkSVZ6k4+bka5dWpEpAdCQVOA='
where username = 'admin';

update app_users
set password_hash = 'pbkdf2-sha256$120000$b3BlcmF0b3Itc2FsdC0yMDI2$w4aULcGDBGSRgpRBA74Iikl/qi7y4cLSH6kRkwVae8A='
where username = 'operator';
