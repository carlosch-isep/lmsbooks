import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
    vus: 5,
    duration: '30s',
};

export default function () {
    let payload = JSON.stringify({
        title: `Book ${__VU}-${__ITER}`,
        author: "Test Author",
        isbn: `978-3-16-148410-${__VU}`,
        description: "Load test book"
    });

    let params = {
        headers: { 'Content-Type': 'application/json' },
    };

    let res = http.post('http://localhost:8087/books', payload, params);
    check(res, { 'created': (r) => r.status === 201 });
    sleep(1);
}
