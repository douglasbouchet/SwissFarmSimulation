class x(v: Boolean) {
  def get: Boolean = v
  override def toString = v.toString
}


val q = new x(true)

print(q)