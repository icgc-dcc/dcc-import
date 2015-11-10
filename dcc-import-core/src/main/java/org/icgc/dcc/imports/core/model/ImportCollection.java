/*
 * Copyright (c) 2013 The Ontario Institute for Cancer Research. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.icgc.dcc.imports.core.model;

import static lombok.AccessLevel.PRIVATE;

import org.icgc.dcc.common.core.model.Identifiable;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * Types of Collections for DB Import, corresponds to collections in MongoDB
 */

@Getter
@RequiredArgsConstructor(access = PRIVATE)
public enum ImportCollection implements Identifiable {

  PROJECTS("Projects", ImportSource.PROJECTS),
  CGC("CGC", ImportSource.CGC),
  GO("GO", ImportSource.GO),
  PATHWAYS("Pathways", ImportSource.PATHWAYS),
  GENES("Genes", ImportSource.GENES),
  DRUGS("Drugs", ImportSource.DRUGS),
  DIAGRAMS("Diagrams", ImportSource.DIAGRAMS);

  @NonNull
  private final String id;

  @NonNull
  private final ImportSource source;

  public static ImportCollection forSource(@NonNull ImportSource source) {
    for (val value : values()) {
      if (source == value.getSource()) {
        return value;
      }
    }

    return null;
  }

  public static ImportCollection byName(@NonNull String name) {
    for (val value : values()) {
      if (name.equalsIgnoreCase(value.name())) {
        return value;
      }
    }

    throw new IllegalArgumentException("No '" + ImportCollection.class.getName() + "' value with name '" + name
        + "' found");
  }

}
