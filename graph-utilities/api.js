const bodyParser = require('body-parser');
const express = require('express'),
    app = express(),
    port = 5000;
const Graph = require('/Users/armanmac/Documents/capstone/graph-utilities/schemas/graph.js');
const mongoose = require('mongoose');

mongoose.connect('mongodb://localhost:27017/graph');
app.use(bodyParser.json());

app.listen(port, () => {
    console.log(`server running on port ${port}`)
});

app.get('/html', (req, res) => {
    res.sendFile('/Users/armanmac/Documents/capstone/graph-utilities/graph.html')
});

app.post('/addGraph', (req, res) => {

    let nodes = req.body.nodes.map(node => {
        return {
            id: node.id,
            shape: "circularImage",
            image: "/Users/armanmac/Documents/capstone/graph-utilities/images/dollar.png",
            label: node.name,
            size: 50
        }
    });

    let edges = req.body.edges.map(edge => {
        return {
            from: edge.from,
            to: edge.to,
            value: edge.value
        }
    });

    let graph = new Graph({
        nodes: nodes,
        edges: edges
    });

    graph.save((err, obj) => {
        if (err) res.status().send({
            "message": "fail to write"
        });

        res.status(200).send({
            "message": "successfully added to db",
            "id": obj._id
        })
    });


});

app.get('/getGraph', (req, res) => {
    Graph.findOne({}).sort({date: -1}).exec((err, graph) => {
        res.status(200).send({
            graph: graph
        })
    })
});








