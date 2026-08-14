create
or replace
function actual_patch()
returns text
language sql
stable
as $$
select patch
from matches
order by match_date desc limit 1;

$$;
