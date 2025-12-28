import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
    vus: 10,
    duration: '1m',
};

export default function () {
    // GET books
    let res = http.get('http://localhost:8087/books');
    check(res, { 'GET status 200': (r) => r.status === 200 });

    // POST book
    let payload = JSON.stringify({
        title: `Book ${__VU}-${__ITER}`,
        author: "Test Author",
        isbn: `978-3-16-148410-${__VU}`,
        description: "Load test book"
    });
    let params = { headers: { 'Content-Type': 'application/json' } };
    let postRes = http.post('http://localhost:8087/books', payload, params);
    check(postRes, { 'POST status 201': (r) => r.status === 201 });

    sleep(1);
}
