package model.entities

import org.scalatest.funsuite.AnyFunSuite
import utils.Vector2D

class TestWall extends AnyFunSuite:
  val sampleWall: Wall = Wall("wall1", Vector2D(10, 10), 100, 50)

  test("A Wall should have correct initial properties"):
    assert(sampleWall.id == "wall1")
    assert(sampleWall.position == Vector2D(10, 10))
    assert(sampleWall.width == 100)
    assert(sampleWall.height == 50)

  test("contains should detect points inside the wall"):
    assert(sampleWall.contains(Vector2D(10, 10))) // Top-left corner
    assert(sampleWall.contains(Vector2D(110, 60))) // Bottom-right corner
    assert(sampleWall.contains(Vector2D(50, 30))) // Center point

  test("contains should detect points outside the wall"):
    assert(!sampleWall.contains(Vector2D(9, 10))) // Left of wall
    assert(!sampleWall.contains(Vector2D(10, 9))) // Above wall
    assert(!sampleWall.contains(Vector2D(111, 60))) // Right of wall
    assert(!sampleWall.contains(Vector2D(110, 61))) // Below wall
    assert(!sampleWall.contains(Vector2D(0, 0))) // Far outside

  test("collidesWith should detect collisions with other entities"):
    case class TestEntity(position: Vector2D) extends Positioned

    assert(sampleWall.collidesWith(TestEntity(Vector2D(50, 30))))
    assert(sampleWall.collidesWith(TestEntity(Vector2D(100, 50))))

    assert(!sampleWall.collidesWith(TestEntity(Vector2D(111, 10))))
    assert(!sampleWall.collidesWith(TestEntity(Vector2D(10, 61))))
    assert(!sampleWall.collidesWith(TestEntity(Vector2D(0, 0))))

  test("withLength should create a new wall with updated width"):
    val widerWall = sampleWall.withSize(150)
    assert(widerWall.width == 150)
    assert(widerWall.height == 50)
    assert(widerWall.position == sampleWall.position)
    assert(widerWall.id == sampleWall.id)

    // Original wall unchanged
    assert(sampleWall.width == 100)

  test("withHeight should create a new wall with updated height"):
    val tallerWall = sampleWall.withHeight(75)
    assert(tallerWall.height == 75)
    assert(tallerWall.width == 100)
    assert(tallerWall.position == sampleWall.position)
    assert(tallerWall.id == sampleWall.id)

    // Original wall unchanged
    assert(sampleWall.height == 50)

  test("withSize should create a new wall with updated dimensions"):
    val biggerWall = sampleWall.withSize(120, 60)
    assert(biggerWall.width == 120)
    assert(biggerWall.height == 60)
    assert(biggerWall.position == sampleWall.position)
    assert(biggerWall.id == sampleWall.id)

    // Original wall unchanged
    assert(sampleWall.width == 100)
    assert(sampleWall.height == 50)

  test("Size changes should maintain immutability"):
    val original = sampleWall
    val modified = original
      .withSize(150)
      .withHeight(75)
      .withSize(200, 100)

    assert(original.width == 100)
    assert(original.height == 50)
    assert(modified.width == 200)
    assert(modified.height == 100)

  test("Wall should not collide with entities at non-overlapping positions"):
    case class FarEntity(position: Vector2D) extends Positioned

    val distantEntity = FarEntity(Vector2D(200, 200))
    val edgeEntity = FarEntity(Vector2D(111, 10))

    assert(!sampleWall.collidesWith(distantEntity))
    assert(!sampleWall.collidesWith(edgeEntity))

  test("Wall should detect collisions at edge positions"):
    case class EdgeEntity(position: Vector2D) extends Positioned

    val leftEdgeEntity = EdgeEntity(Vector2D(10, 10)) // Top-left corner
    val rightEdgeEntity = EdgeEntity(Vector2D(110, 10)) // Top-right edge
    val bottomEdgeEntity = EdgeEntity(Vector2D(10, 60)) // Bottom-left edge

    assert(sampleWall.collidesWith(leftEdgeEntity))
    assert(sampleWall.collidesWith(rightEdgeEntity))
    assert(sampleWall.collidesWith(bottomEdgeEntity))
