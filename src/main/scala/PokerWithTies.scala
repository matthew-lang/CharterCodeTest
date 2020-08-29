object PokerWithTies extends App {

  /*
   * Given a set of 5 playing card identifiers such as 2H, 7C, QS, 10D, 2D;
   * determine if this hand is better than some other hand, according to the rules of poker.
   *
   * Hands will be a string with 5 cards comma separated,
   * each card will have 1-2 digits or JQKA and a suit indicator C,D,S,H (i.e. 10C, KH)
   *
   * Possible Hand Types Below:
   *   Straight flush
   *   Four of a kind
   *   Full house
   *   Flush
   *   Straight
   *   Three of a kind
   *   Two pair
   *   One pair
   *
   * The goal of this is to compare between the hand types.
   * Comparing 2 of the same type (i.e. 2 straights) to determine a winner is outside the scope
   * and will not be tested.
   *
   * Implement hand1WinsOverHand2 method and return whether or not the first hand wins over the second hand.
   */

  case class PokerCard(value: Int, suit: String)
  case class HandValue(value: Int, kickerValues: Seq[Int])

  val HIGH_CARD = 0
  val ONE_PAIR = 100
  val TWO_PAIR = 200
  val THREE_OF_KIND = 300
  val STRAIGHT = 400
  val FLUSH = 500
  val FULL_HOUSE = 600
  val FOUR_OF_KIND = 700
  val STRAIGHT_FLUSH = 800

  def hand1WinsOverHand2(hand1Str: String, hand2Str: String): Boolean = {
    val hand1 = getCards(hand1Str)
    val hand2 = getCards(hand2Str)
    val handValue1 = getHandValue(hand1)
    val handValue2 = getHandValue(hand2)
    if(handValue1.value > handValue2.value) {
      true
    } else if(handValue1.value < handValue2.value) {
      false
    } else {
      //println(s"we have a tie between ${handValue1} and ${handValue2}, going to kicker cards for tie breaker.")
      handValue1.kickerValues
        .zip(handValue2.kickerValues)
        .filterNot {
          case (h1, h2) => h1 == h2
        }
        .headOption
        .map {
          case (h1, h2) => h1 > h2
        }
        .getOrElse(false)
    }
  }

  //https://www.adda52.com/poker/poker-rules/cash-game-rules/tie-breaker-rules
  private def getHandValue(hand: Seq[PokerCard]): HandValue = {
    val isFlush = hand.map(_.suit).toSet.size == 1
    val isStraight = calculateIsStraight(hand)
    val valueCounts = hand.groupBy(_.value).values
    val pairs = valueCounts.filter(_.size == 2).toSeq.sortBy(_.head.value)
    val threeKind = valueCounts.filter(_.size == 3).flatten.toSeq
    val fourKind = valueCounts.filter(_.size == 4).flatten.toSeq
    val highest = hand.maxBy(_.value)

    if(isFlush && isStraight) {
      HandValue(value = STRAIGHT_FLUSH + highest.value, Seq.empty)
    } else if(fourKind.nonEmpty) {
      HandValue(value = FOUR_OF_KIND + fourKind.head.value, kickerValues = getKickerCards(hand, fourKind).map(_.value))
    } else if(threeKind.nonEmpty && pairs.nonEmpty) {
      HandValue(value = FULL_HOUSE + threeKind.head.value, kickerValues = Seq(pairs.flatten.head.value))
    }else if(isFlush) {
      HandValue(value = FLUSH, kickerValues = hand.sortBy(_.value).map(_.value))
    }else if (isStraight) {
      HandValue(value = STRAIGHT + highest.value, kickerValues = Seq.empty)
    }else if(threeKind.nonEmpty) {
      HandValue(value = THREE_OF_KIND + threeKind.head.value, kickerValues = getKickerCards(hand, threeKind).map(_.value))
    }else if(pairs.size == 2) {
      HandValue(value = TWO_PAIR + pairs.head.head.value, kickerValues = (Seq(pairs.last.head.value) ++ getKickerCards(hand,pairs.flatten).map(_.value)))
    } else if(pairs.size == 1) {
      HandValue(value = ONE_PAIR + pairs.head.head.value, kickerValues = getKickerCards(hand,pairs.flatten).map(_.value))
    }else {
      HandValue(value = HIGH_CARD + highest.value, getKickerCards(hand, Seq(highest)).map(_.value))
    }
  }

  private def getKickerCards(hand: Seq[PokerCard], usedCards: Seq[PokerCard]) = {
    hand.filterNot(usedCards.contains(_)).sortBy(_.value).reverse
  }

  private def calculateIsStraight(hand: Seq[PokerCard]): Boolean = {
    val values = hand.sortBy(_.value).map(_.value)
    val min = values.min
    val max = values.max
    values == (min to max).toSeq
  }

  private def getCards(hand: String): Seq[PokerCard] = hand.split(",").map(cardStr => {
    val mtch = "(\\d{1,2}|[JQKA])([CDSH])".r.findAllIn(cardStr).matchData.toSeq.headOption.getOrElse(throw new IllegalStateException("Unable to match string for card value/suit"))
    val cardValueStr = mtch.group(1)
    val suit = mtch.group(2)
    val cardValue = cardValueStr.toIntOption match {
      case Some(num) => num
      case None =>
        cardValueStr match {
          case "J" => 11
          case "Q" => 12
          case "K" => 13
          case "A" => 14
          case _ => 0
        }
    }

    if(cardValue <= 1 || cardValue > 14){
      throw new IllegalStateException(s"Invalid card value: ${cardValue}")
    }
    PokerCard(value = cardValue, suit)
  })

  implicit class CompareTwoPokerHands(hand1: String) {
    def winsOver(hand2: String): Unit = {
      val result = if (hand1WinsOverHand2(hand1, hand2)) "Correct" else "Incorrect"
      println(s"$result, hand [$hand1] wins over [$hand2]")
    }
  }

  println("Poker Hand comparison")
  "8C,9C,10C,JC,QC" winsOver "6S,7H,8D,9H,10D" // straight flush
  "4H,4D,4C,4S,JS" winsOver "6C,6S,KH,AS,AD" // four of a kind
  "5C,3C,10C,KC,7C" winsOver "6C,6D,6H,9C,KD" // flush
  "4H,4D,4C,KC,KD" winsOver "9D,6S,KH,AS,AD" // full house
  "2C,3C,4S,5S,6S" winsOver "6C,6D,6H,9C,KD" // straight
  "7C,7D,7S,3H,4D" winsOver "9S,6S,10D,AS,AD" // three of a kind
  "8C,8H,10S,KH,KS" winsOver "2S,2D,JH,7S,AC" // two pair
  "AC,AH,3C,QH,10C" winsOver "3S,2D,KH,JS,AD" // one pair

  //more test cases with ties
  "AC,AH,3C,QH,10C" winsOver "AS,2D,3H,JS,AD" // one pair
  "KH,KD,KC,4C,4D" winsOver "KH,KD,KC,2C,2D" // full house
  "8C,9C,10C,JC,QC" winsOver "7C,8C,9C,10C,JC" // straight flush
  "4H,4D,4C,4S,JS" winsOver "4H,4D,4C,4S,JS" // four of a kind

  System.exit(0)
}