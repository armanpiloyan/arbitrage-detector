import scala.collection.mutable.ListBuffer

object Driver {
  def main(args: Array[String]): Unit = {


    val V = 5
    val E = 8
    val graph = new Graph(V, E)

    graph.Edge.head.src = 0
    graph.Edge.head.dest = 1
    graph.Edge.head.weight = -1.0

    graph.Edge(1).src = 0
    graph.Edge(1).dest = 2
    graph.Edge(1).weight = 4.0

    graph.Edge(2).src = 1
    graph.Edge(2).dest = 2
    graph.Edge(2).weight = 3.0

    graph.Edge(3).src = 1
    graph.Edge(3).dest = 3
    graph.Edge(3).weight = 2.0

    graph.Edge(4).src = 1
    graph.Edge(4).dest = 4
    graph.Edge(4).weight = 2.0

    graph.Edge(5).src = 3
    graph.Edge(5).dest = 2
    graph.Edge(5).weight = 5.0

    graph.Edge(6).src = 3
    graph.Edge(6).dest = 1
    graph.Edge(6).weight = 1.0

    graph.Edge(7).src = 4
    graph.Edge(7).dest = 3
    graph.Edge(7).weight = -3.0

    graph.BellmanFord(graph, 0)

    println("----------------------")

    val V1 = 3
    val E1 = 3
    val graph1 = new Graph(V1, E1)

    graph1.Edge.head.src = 0
    graph1.Edge.head.dest = 1
    graph1.Edge.head.weight = 1.0

    graph1.Edge(1).src = 1
    graph1.Edge(1).dest = 2
    graph1.Edge(1).weight = 1.0

    graph1.Edge(2).src = 2
    graph1.Edge(2).dest = 0
    graph1.Edge(2).weight = -3.0

    graph1.BellmanFord(graph1, 0)


    //Data scrapping
    val dataCollector = new DataCollector()
    val rates = dataCollector.getExchangeRates(
      "https://min-api.cryptocompare.com/data/price",
      "LTC",
      ListBuffer("USD", "NMC", "GRC", "XPM", "EUR", "GBP")
    )

    println()
    print(rates)


    val currencyList: Array[String] = (
      "BTC", "LTC", "NMC", "PPC", "XDG", "GRC", "XPM", "XRP", "NXT", "AUR", "DASH", "NEO", "MZC", "XMR", "XEM", "POT",
      "TIT", "XVG", "XLM", "VTC", "ETH", "ETC", "USDT", "ZEC", "BCH", "EOS", "AFN", "EUR", "ALL", "DZD", "USD", "AOA",
      "XCD", "ARS", "AMD", "AWG", "AUD", "AZN", "BSD", "BHD", "BDT", "BBD", "BYN", "BZD", "XOF", "BMD", "INR", "BTN",
      "BOB", "BOV", "BAM", "BWP", "NOK", "BRL", "BND", "BGN", "BIF", "CVE", "KHR", "XAF", "CAD", "KYD", "CLP", "CLF",
      "CNY", "COP", "COU", "KMF", "CDF", "NZD", "CRC", "HRK", "CUP", "CUC", "ANG", "CZK", "DKK", "DJF", "DOP", "EGP",
      "SVC", "ERN", "ETB", "FKP", "FJD", "XPF", "GMD", "GEL", "GHS", "GIP", "GTQ", "GBP", "GNF", "GYD", "HTG", "HNL",
      "HKD", "HUF", "ISK", "IDR", "XDR", "IRR", "IQD", "ILS", "JMD", "JPY", "JOD", "KZT", "KES", "KPW", "KRW", "KWD",
      "KGS", "LAK", "LBP", "LSL", "ZAR", "LRD", "LYD", "CHF", "MOP", "MKD", "MGA", "MWK", "MYR", "MVR", "MRU", "MUR",
      "XUA", "MXN", "MXV", "MDL", "MNT", "MAD", "MZN", "MMK", "NAD", "NPR", "NIO", "NGN", "OMR", "PKR", "PAB", "PGK",
      "PYG", "PEN", "PHP", "PLN", "QAR", "RON", "RUB", "RWF", "SHP", "WST", "STN", "SAR", "RSD", "SCR", "SLL", "SGD",
      "XSU", "SBD", "SOS", "SSP", "LKR", "SDG", "SRD", "SZL", "SEK", "CHE", "CHW", "SYP", "TWD", "TJS", "TZS", "THB",
      "TOP", "TTD", "TND", "TRY", "TMT", "UGX", "UAH", "AED", "USN", "UYU", "UYI", "UYW", "UZS", "VUV", "VES", "VND",
      "YER", "ZMW", "ZWL"

    )


  }


  def constructGraph(includeCurrencies: Array[Int], treshhold: Double, currencyList: Array[String]): Graph = {
    val dataCollector = new DataCollector()
    val V = includeCurrencies.length
    var E = 0
    val matrix = Array.ofDim[Double](V, V)

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
      for (col <- 0 until V) {

        if (matrix(row)(col) > treshhold) {

          graph.Edge(edgeCount).src = row
          graph.Edge(edgeCount).dest = col

          val rate = dataCollector.getExchangeRates(
            "https://min-api.cryptocompare.com/data/price",
            currencyList(includeCurrencies(row)),
            ListBuffer(currencyList(includeCurrencies(col)))
          )(currencyList(includeCurrencies(col)))

          graph.Edge(edgeCount).weight = rate
          edgeCount += 1
        }

      }
    }
    return graph
  }

  def giveProb(source: Int, dest: Int): Double = {
    return 0.6
  }

  def sendGraph(graph: Graph): Boolean = {


    return true
  }


}
