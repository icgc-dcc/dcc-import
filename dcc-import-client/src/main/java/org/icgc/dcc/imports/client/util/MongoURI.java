/*
 * Copyright (c) 2016 The Ontario Institute for Cancer Research. All rights reserved.
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
package org.icgc.dcc.imports.client.util;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.io.IOException;
import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.net.UnknownHostException;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;
import javax.validation.constraints.NotNull;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

import lombok.val;

@NotNull(message = "URI must not be null")
@Target(value = FIELD)
@Retention(value = RUNTIME)
@Inherited
@Documented
@Constraint(validatedBy = MongoURI.Validator.class)
@ReportAsSingleViolation
public @interface MongoURI {

  String message()

  default "Bad Mongo URI";

  Class<?>[]groups() default {};

  Class<? extends Payload>[]payload() default {};

  /**
   * Validator that is invoked when annotation is applied.
   */
  static class Validator implements ConstraintValidator<MongoURI, MongoClientURI> {

    @Override
    public void initialize(MongoURI constraintAnnotation) {
    }

    @Override
    public boolean isValid(MongoClientURI value, ConstraintValidatorContext context) {
      val database = value.getDatabase();
      if (isNullOrEmpty(database)) {
        fail(context, "Database is missing");

        return false;
      }

      val collection = value.getCollection();
      if (!isNullOrEmpty(collection)) {
        fail(context, "Collection should not be specified");

        return false;
      }

      try {
        val mongo = new MongoClient(value);
        try {
          // Test connectivity
          val socket = mongo.getMongoOptions().socketFactory.createSocket();
          socket.connect(mongo.getAddress().getSocketAddress());

          // All good
          socket.close();
        } catch (IOException ex) {
          fail(context, "Could not connect to host(s) '" + value.getHosts() + "': " + ex.getMessage());

          return false;
        } finally {
          mongo.close();
        }
      } catch (UnknownHostException e) {
        fail(context, "Unknown host: " + e.getMessage());

        return false;
      }

      return true;
    }

    private static void fail(ConstraintValidatorContext context, String message) {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }

  }

}
