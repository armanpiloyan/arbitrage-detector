
import net.liftweb.json._

import scala.collection.mutable.ListBuffer
import scala.reflect.runtime.universe._
import scala.reflect.{ClassTag, ManifestFactory}


class DataCollector {


  case class Value(value: Double)

  def getExchangeRates(
                           url: String,
                           accordingTo: String,
                           relativeTo: ListBuffer[String]
                         ): collection.mutable.Map[String, Double] = {

    var params = collection.mutable.Map[String, String]()
    var convertTo = ""

    for (curr <- relativeTo) {
      convertTo = convertTo.concat(curr).concat(",")
    }

    convertTo = convertTo.dropRight(1)
    params += ("fsym" -> accordingTo)
    params += ("tsyms" -> convertTo)

    val req = requests.get(
      url,
      params = params

    )


    var rates = collection.mutable.Map[String, Double]()
    for (curr <- relativeTo) {
      rates += (curr ->
        (parse(req.text) \ curr).extract[Double](net.liftweb.json.DefaultFormats, toManifest[Double])
        )
    }
    return rates
  }

  def toManifest[T: TypeTag]: Manifest[T] = {
    val t = typeTag[T]
    val mirror = t.mirror

    def toManifestRec(t: Type): Manifest[_] = {
      val clazz = ClassTag[T](mirror.runtimeClass(t)).runtimeClass
      if (t.typeArgs.length == 1) {
        val arg = toManifestRec(t.typeArgs.head)
        ManifestFactory.classType(clazz, arg)
      } else if (t.typeArgs.length > 1) {
        val args = t.typeArgs.map(x => toManifestRec(x))
        ManifestFactory.classType(clazz, args.head, args.tail: _*)
      } else {
        ManifestFactory.classType(clazz)
      }
    }

    toManifestRec(t.tpe).asInstanceOf[Manifest[T]]
  }


}
