package net.agileautomata.executor4s

package object testing {
  implicit def convertIntToDecoratedInteger(i: Int) = new DecoratedInteger(i)
}