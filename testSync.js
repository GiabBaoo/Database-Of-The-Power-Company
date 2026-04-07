// testSync.js
const { syncSQLServer } = require('./src/Config/DBConnection');

(async () => {
    try {
        console.log("Bắt đầu sync...");
        await syncSQLServer();
        console.log("Sync xong!");
    } catch (err) {
        console.error("Lỗi khi sync:", err);
    }
})();