const jwt = require('jsonwebtoken');

module.exports = function verifyToken(req, res, next) {
    const token = req.headers['authorization'];
    console.log("Auth header:", req.headers);
    console.log("Extracted token:", token);

    if (!token) {
        return res.status(401).json({ message: 'No token provided' });
    }
    jwt.verify(token, process.env.JWT_SECRET, (err, decoded) => {
        if (err) {
            return res.status(401).json({ message: 'Failed to authenticate token' });
        }
        req.userId = decoded.id;
        next();
    });
};
