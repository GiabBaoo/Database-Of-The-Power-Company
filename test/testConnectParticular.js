const sql = require("mssql");
require("dotenv").config();
sql.connect({
    user: "sa",
    password: "",
    server: "",

    database: "DienLuc",
    options: {
        trustServerCertificate: true,
        // instanceName: "1436",  // hoặc port: 1433
        encrypt: true,                 // bắt buộc
        trustServerCertificate: true,  // bắt buộc khi dùng IP
        enableArithAbort: true,

    }
})
    .then(pool => {
        console.log("Connected!");
    })
    .catch(err => {
        console.error("Error:", err);
    });
