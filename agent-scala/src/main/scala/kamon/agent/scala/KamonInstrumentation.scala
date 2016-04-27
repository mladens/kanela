package kamon.agent.scala

import java.util.function.{ BiFunction ⇒ JBifunction, Supplier ⇒ JSupplier }

import kamon.agent.api.instrumentation.InstrumentationDescription
import kamon.agent.libs.javaslang.{ Function1 ⇒ JFunction1, Function2 ⇒ JFunction2, Function3 ⇒ JFunction3 }
import kamon.agent.libs.net.bytebuddy.description.`type`.TypeDescription
import kamon.agent.libs.net.bytebuddy.description.method.MethodDescription
import kamon.agent.libs.net.bytebuddy.dynamic.DynamicType.Builder
import kamon.agent.libs.net.bytebuddy.implementation.MethodDelegation
import kamon.agent.libs.net.bytebuddy.matcher.ElementMatcher.Junction

trait KamonInstrumentation extends kamon.agent.api.instrumentation.KamonInstrumentation {

  implicit def toJavaSupplier[A](f: ⇒ A): JSupplier[A] = new JSupplier[A] {
    override def get(): A = f
  }

  implicit def toJavaFunction1[A, B](f: (A) ⇒ B): JFunction1[A, B] = new JFunction1[A, B] {
    override def apply(a: A): B = f(a)
  }

  private implicit def toJavaFunction2[A, B, C](f: (A, B) ⇒ C): JFunction2[A, B, C] = new JFunction2[A, B, C] {
    override def apply(a: A, b: B): C = f(a, b)
  }

  private implicit def toJavaFunction3[A, B, C, D](f: (A, B, C) ⇒ D): JFunction3[A, B, C, D] = new JFunction3[A, B, C, D] {
    override def apply(a: A, b: B, c: C): D = f(a, b, c)
  }

  private implicit def toJavaBiFunction[A, B, C](f: (A, B) ⇒ C): JBifunction[A, B, C] = new JBifunction[A, B, C] {
    override def apply(a: A, b: B): C = f(a, b)
  }

  def forSubtypeOf(name: String)(builder: InstrumentationDescription.Builder ⇒ InstrumentationDescription) = {
    super.forSubtypeOf(name, builder)
  }

  def forTargetType(name: String)(builder: InstrumentationDescription.Builder ⇒ InstrumentationDescription) = {
    super.forTargetType(name, builder)
  }

  implicit class PimpInstrumentationBuilder(instrumentationBuilder: InstrumentationDescription.Builder) {
    def withTransformationFor(method: Junction[MethodDescription], delegate: Class[_]) = {
      addTransformation((builder, _, _) ⇒ builder.method(method).intercept(MethodDelegation.to(delegate)))
    }
    def addTransformation(f: ⇒ (Builder[_], TypeDescription, ClassLoader) ⇒ Builder[_]) = instrumentationBuilder.withTransformation(f)
  }
}