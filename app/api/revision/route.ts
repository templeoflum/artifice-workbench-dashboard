import revision from '../../revision.json';
export function GET(){return Response.json(revision,{headers:{'Cache-Control':'no-store'}})}
