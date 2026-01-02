import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
    duration: '10s',
    vus: 2,
};

export default function () {
    let res = http.get('http://localhost:8087/api/books');
    check(res, { 'status was 200': (r) => r.status === 200 });
    sleep(1);
}
