/**
 * Copyright 2011 John Adam Crain (jadamcrain@gmail.com)
 *
 * This file is the sole property of the copyright owner and is NOT
 * licensed to any 3rd parties.
 */
package net.agileautomata.commons

package object testing {
  implicit def convertIntToDecoratedInt(i: Int) = new DecoratedInteger(i)
}