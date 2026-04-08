const winston = require("winston");
require("winston-daily-rotate-file");

function createCityLogger(city) {
    return winston.createLogger({
        levels: {
            insert: 0,
            update: 1,
            delete: 2,
            info: 3,
            warn: 4,
            error: 5,
        },
        format: winston.format.combine(
            winston.format.timestamp(),
            winston.format.printf(({ level, message, timestamp, ...meta }) => {
                return JSON.stringify({
                    timestamp,
                    level,
                    message,
                    city,
                    ...meta
                });
            })
        ),
        transports: [
            new winston.transports.DailyRotateFile({
                dirname: "D:/loki-logging/logs",
                filename: `${city}-%DATE%.log`,
                datePattern: "YYYY-MM-DD",
            })
        ]
    });
}

module.exports = {
    tp1: createCityLogger("TP1"),
    tp2: createCityLogger("TP2"),
    tp3: createCityLogger("TP3")
};
