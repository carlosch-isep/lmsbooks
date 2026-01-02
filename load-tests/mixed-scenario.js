import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
    vus: 10,
    duration: '1m',
};

export default function () {
    // POST book
    const TITLE = `Book ${__VU}-${__ITER}`;
    let payload = JSON.stringify({
        title: "${TITLE}",
        author: "Test Author",
        isbn: `978316148410${__VU}`,
        description: "Load test book"
    });
    let params = { headers: { 'Content-Type': 'application/json' } };
    let postRes = http.post('http://localhost:8088/api/books', payload, params);
    check(postRes, { 'POST status 201': (r) => r.status === 201 });


    // GET books
    let res = http.get('http://localhost:8087/api/books');
    check(res, { 'GET status 200': (r) => r.status === 200 });

    sleep(1);
}
