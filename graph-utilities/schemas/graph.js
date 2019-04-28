const mongoose = require('mongoose');
const Schema = mongoose.Schema;

const blogSchema = new Schema({
    nodes: [
        {
            id: Number,
            shape: String,
            image: String,
            label: String,
            size: Number
        }
    ],
    edges: [
        {
            from: Number,
            to: Number,
            value: Number
        }
    ],
    date: {
        type: Date,
        default: Date.now
    }
});


module.exports = mongoose.model('Graph', blogSchema);