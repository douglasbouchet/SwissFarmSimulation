class x(v: Boolean) {
  def get: Boolean = v
}


val q = new x(true)
val w = new x(true)
val z = new x(true)
val u = new x(true)
val e = new x(false)


val l = List(q,w,e)

l.forall(_.get)