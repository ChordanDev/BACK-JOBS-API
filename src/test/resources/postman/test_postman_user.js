pm.test("Response status is 200 OK", function () {
    pm.response.to.have.status(200);
});

pm.test("Top-level response types are correct", function () {
    const json = pm.response.json();

    pm.expect(json.page).to.be.a("number");
    pm.expect(json.per_page).to.be.a("number");
    pm.expect(json.total).to.be.a("number");
    pm.expect(json.total_pages).to.be.a("number");
    pm.expect(json.data).to.be.an("array");
});

pm.test("Each user has required fields", function () {
    const json = pm.response.json();

    json.data.forEach((user) => {
        pm.expect(user.id).to.be.a("number");
        pm.expect(user.email).to.be.a("string");
        pm.expect(user.first_name).to.be.a("string");
        pm.expect(user.last_name).to.be.a("string");
        pm.expect(user.avatar).to.be.a("string");
    });
});

pm.test("User IDs are sequential", function () {
    const json = pm.response.json();

    json.data.forEach((user, index) => {
        pm.expect(user.id).to.equal(index + 1);
    });
});
