import scala.collection.mutable.ListBuffer
import ujson.{Obj, Value}

import scala.util.Random
import org.apache.spark.SparkContext
import org.apache.spark.SparkConf

import scala.collection.mutable

case class CustomPair(graph: Graph, profit: Double, subset: Array[Int])


object Driver {
  def main(args: Array[String]): Unit = {

    val conf: SparkConf = new SparkConf().setAppName("CryptoMaster").setMaster("local")
    val sc: SparkContext = new SparkContext(conf)


    //    val testCurrencyList = Array[String]("USD", "CAD", "JPY", "EUR", "CNY")
    //    val testCurrencyInd = Array[Int](0, 1, 2, 3, 4)

    val random = new Random()
    val subsets = new ListBuffer[Array[Int]]

    val currencyList = Array[String](
      "USD", "JPY", "BGN", "CZK", "DKK", "GBP", "HUF", "PLN", "RON", "SEK", "CHF", "ISK", "NOK",
      "HRK", "RUB", "TRY", "AUD", "BRL", "CAD", "CNY", "HKD", "IDR", "ILS", "INR", "KRW", "MXN",
      "MYR", "NZD", "PHP", "SGD", "THB", "ZAR"
    )

    //    val currencyList = Array[String](
    //      "BTC", "LTC", "NMC", "PPC", "XDG", "GRC", "XPM", "XRP", "NXT", "AUR", "DASH", "NEO", "MZC", "XMR", "XEM",
    //      "POT", "TIT", "XVG", "XLM", "VTC", "ETH", "ETC", "USDT", "ZEC", "BCH", "EOS", "AFN", "EUR", "ALL", "DZD", "USD",
    //      "GBP", "AUD", "EUR", "INR", "ARS", "BRL", "CAD", "CNY", "NZD", "DKK", "HKD", "ILS", "JPY", "KES", "CHF", "MXN",
    //      "NOK", "PHP", "PLN", "SGD", "SEK", "AED"
    //    )


    for (_ <- 0 until 40) {
      subsets += (for (_ <- 1 to 20) yield random.nextInt(currencyList.length - 1)).toArray
    }

    val subsetsParallel = sc.parallelize(subsets)

    val bestCaseGraph = subsetsParallel.map(
      subset => {
        val graph = constructGraph(subset, 0.5, currencyList)
        CustomPair(graph, sparkMapper(graph), subset)
      }
    ).reduce((a, b) => {
      if (a.profit > b.profit)
        a
      else
        b
    })

    println("Sending most profitable graph...")

    postGraph(
      graph = bestCaseGraph.graph,
      includeCurrencies = bestCaseGraph.subset,
      currencyList = currencyList
    )


  }

  def sparkMapper(graph: Graph): Double = {
    var arbitrageValue = 1.0
    graph.BellmanFord(graph, 0)
    if (graph.cycle.nonEmpty) {
      for (i <- graph.cycle.indices) {
        if (i != graph.cycle.length - 1) {
          graph.Edge.foreach(edge => {
            if (edge.src == graph.cycle(i) && edge.dest == graph.cycle(i + 1)) {
              arbitrageValue *= edge.weightReal
            }
          })
        }
      }
      arbitrageValue
    }
    else 0.0
  }


  def constructGraph(includeCurrencies: Array[Int], treshhold: Double, currencyList: Array[String]): Graph = {
    val dataCollector = new DataCollector()
    val V = includeCurrencies.length
    var E = 0
    val matrix = Array.ofDim[Double](V, V)

    for (row <- 0 until V) {
      for (col <- 0 until V) {
        matrix(row)(col) = 0
      }
    }

    for (row <- 0 until V) {
      for (col <- 0 until V) {
        val currProb = giveProb(includeCurrencies(row), includeCurrencies(col))
        if (currProb > treshhold)
          E += 1

        matrix(row)(col) = currProb
      }
    }

    val graph = new Graph(V, E)
    var edgeCount = 0

    for (row <- 0 until V) {
      for (col <- 1 until V) {
        if (matrix(row)(col) > treshhold) {
          graph.Edge(edgeCount).src = row
          graph.Edge(edgeCount).dest = col

          //          val rate = dataCollector.getExchangeRates(
          //            "https://min-api.cryptocompare.com/data/price",
          //            currencyList(includeCurrencies(row)),
          //            ListBuffer(currencyList(includeCurrencies(col)))
          //          )(currencyList(includeCurrencies(col)))

          val rate = dataCollector
            .getExchageRatesReal(
              "https://api.exchangeratesapi.io/latest",
              currencyList(includeCurrencies(row)),
              currencyList(includeCurrencies(col)))(currencyList(includeCurrencies(col)))

          graph.Edge(edgeCount).weight = -math.log(rate)
          graph.Edge(edgeCount).weightReal = rate
          edgeCount += 1
        }
      }
    }
    graph
  }

  def giveProb(source: Int, dest: Int): Double = {
    val r = scala.util.Random
    if (source == dest)
      0
    else
      r.nextFloat
  }

  def postGraph(graph: Graph, includeCurrencies: Array[Int], currencyList: Array[String]): Unit = {


    val nodeList = new ListBuffer[Obj]
    val edgeList = new ListBuffer[Obj]

    for (i <- includeCurrencies.indices) {
      val node: mutable.LinkedHashMap[String, Value] = mutable.LinkedHashMap(
        "id" -> i,
        "name" -> currencyList(includeCurrencies(i)),
        "group" -> (if (graph.cycle.contains(i)) 2 else 1)
      )
      nodeList += new ujson.Obj(node)
    }

    for (edge <- graph.Edge) {
      val edgeObj: mutable.LinkedHashMap[String, Value] = mutable.LinkedHashMap(
        "from" -> edge.src,
        "to" -> edge.dest,
        "value" -> edge.weightReal,
        "color" -> (if (isInCycle(graph.cycle, edge.src, edge.dest)) "yellow" else "white")
      )
      edgeList += new ujson.Obj(edgeObj)
    }


    val r = requests.post(
      url = "http://localhost:5000/addGraph",
      headers = Map("Content-Type" -> "application/json"),
      data = Obj(
        "nodes" -> nodeList,
        "edges" -> edgeList
      ).render())

    print(r)
  }

  def isInCycle(cycle: ListBuffer[Int], src: Int, dest: Int): Boolean = {
    var bool = false
    for (i <- 0 until cycle.length - 1)
      if (cycle(i) == src && cycle(i + 1) == dest)
        bool = true

    bool
  }

  def generateTestGraph(): Graph = {
    val V = 5
    val E = 20
    val graph = new Graph(V, E)

    graph.Edge.head.src = 0
    graph.Edge.head.dest = 1
    graph.Edge.head.weight = -math.log(1.31904)
    graph.Edge.head.weightReal = 1.31904

    graph.Edge(1).src = 1
    graph.Edge(1).dest = 0
    graph.Edge(1).weight = -math.log(0.75799)
    graph.Edge(1).weightReal = 0.75799

    graph.Edge(2).src = 2
    graph.Edge(2).dest = 0
    graph.Edge(2).weight = -math.log(0.00961)
    graph.Edge(2).weightReal = 0.00961

    graph.Edge(3).src = 0
    graph.Edge(3).dest = 2
    graph.Edge(3).weight = -math.log(104.05)
    graph.Edge(3).weightReal = 104.05

    graph.Edge(4).src = 2
    graph.Edge(4).dest = 1
    graph.Edge(4).weight = -math.log(0.01266)
    graph.Edge(4).weightReal = 0.01266

    graph.Edge(5).src = 1
    graph.Edge(5).dest = 2
    graph.Edge(5).weight = -math.log(78.94)
    graph.Edge(5).weightReal = 78.94

    graph.Edge(6).src = 2
    graph.Edge(6).dest = 3
    graph.Edge(6).weight = -math.log(0.00872)
    graph.Edge(6).weightReal = 0.00872

    graph.Edge(7).src = 3
    graph.Edge(7).dest = 2
    graph.Edge(7).weight = -math.log(114.65)
    graph.Edge(7).weightReal = 114.65

    graph.Edge(8).src = 1
    graph.Edge(8).dest = 3
    graph.Edge(8).weight = -math.log(0.68853)
    graph.Edge(8).weightReal = 0.68853

    graph.Edge(9).src = 3
    graph.Edge(9).dest = 1
    graph.Edge(9).weight = -math.log(1.45193)
    graph.Edge(9).weightReal = 1.45193

    graph.Edge(10).src = 1
    graph.Edge(10).dest = 4
    graph.Edge(10).weight = -math.log(5.10327)
    graph.Edge(10).weightReal = 5.10327

    graph.Edge(11).src = 4
    graph.Edge(11).dest = 1
    graph.Edge(11).weight = -math.log(0.19586)
    graph.Edge(11).weightReal = 0.19586

    graph.Edge(12).src = 0
    graph.Edge(12).dest = 4
    graph.Edge(12).weight = -math.log(6.72585)
    graph.Edge(12).weightReal = 6.72585

    graph.Edge(13).src = 4
    graph.Edge(13).dest = 0
    graph.Edge(13).weight = -math.log(0.14864)
    graph.Edge(13).weightReal = 0.14864

    graph.Edge(14).src = 3
    graph.Edge(14).dest = 4
    graph.Edge(14).weight = -math.log(7.41088)
    graph.Edge(14).weightReal = 7.41088

    graph.Edge(15).src = 4
    graph.Edge(15).dest = 3
    graph.Edge(15).weight = -math.log(0.13488)
    graph.Edge(15).weightReal = 0.13488

    graph.Edge(16).src = 4
    graph.Edge(16).dest = 2
    graph.Edge(16).weight = -math.log(15.47)
    graph.Edge(16).weightReal = 15.47

    graph.Edge(17).src = 2
    graph.Edge(17).dest = 4
    graph.Edge(17).weight = -math.log(0.06463)
    graph.Edge(17).weightReal = 0.06463

    graph.Edge(18).src = 0
    graph.Edge(18).dest = 3
    graph.Edge(18).weight = -math.log(1.10185)
    graph.Edge(18).weightReal = 1.10185

    graph.Edge(19).src = 3
    graph.Edge(19).dest = 0
    graph.Edge(19).weight = -math.log(0.90745)
    graph.Edge(19).weightReal = 0.90745

    graph
  }

}
