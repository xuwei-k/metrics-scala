/*
 * Copyright (c) 2013-2018 Erik van Oosten
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.grons.metrics4.scala

import com.codahale.metrics.{MetricRegistry, Reservoir, Histogram => DropwizardHistogram, Timer => DropwizardTimer}
import nl.grons.metrics4.scala.Implicits._
import org.mpierce.metrics.reservoir.hdrhistogram.{HdrHistogramReservoir, HdrHistogramResetOnSnapshotReservoir}

/**
 * An alternative metric builder that creates [[Histogram]]s and [[Timer]]s with
 * [[Reservoir]]s from the HdrHistogram library.
 *
 * See the [[https://github.com/erikvanoosten/metrics-scala/blob/master/docs/Hdrhistogram.md the manual]]
 * for more instructions on using hdrhistogram.
 *
 * @param resetAtSnapshot `false` to use reservoirs that accumulate internal state forever, or
 *                        `true` to use a reservoir that resets its internal state on each snapshot
 *                        (which is how reporters get information from reservoirs).
 *                        See [[http://taint.org/2014/01/16/145944a.html this article]] for when the latter is useful.
 */
class HdrMetricBuilder(
  baseName: MetricName,
  registry: MetricRegistry,
  resetAtSnapshot: Boolean
) extends MetricBuilder(baseName, registry) {

  /**
   * Creates a new histogram metric with a [[Reservoir]] from the HdrHistogram library.
   *
   * @param name the name of the histogram
   * @param scope the scope of the histogram or null for no scope
   */
  override def histogram(name: String, scope: String): Histogram =
    new Histogram(
      registry.histogram(
        metricNameFor(name, scope),
        () => new DropwizardHistogram(createHdrReservoir())))

  /**
   * Creates a new timer metric with a [[Reservoir]] from the HdrHistogram library.
   *
   * @param name the name of the timer
   * @param scope the scope of the timer or null for no scope
   */
  override def timer(name: String, scope: String): Timer =
    new Timer(
      registry.timer(
        metricNameFor(name, scope),
        () => new DropwizardTimer(createHdrReservoir())))

  private def createHdrReservoir(): Reservoir =
    if (resetAtSnapshot) new HdrHistogramResetOnSnapshotReservoir() else new HdrHistogramReservoir()
}