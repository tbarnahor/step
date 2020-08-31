// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

/**
 * Adds a random greeting to the page.
 */
function addRandomPic() {
  const places = ['Alon Hagalil', 'Tel Aviv', 'Mitzpe Ramon', 'Hasbani river', 'Jerusalem', 'Tzipori stream', 'Habonim beach'];
  
  // Pick a random place
  const imageName = places[Math.floor(Math.random() * places.length)];
  // Add image to the page
  const imgElement = createImg(imageName);
  const imageContainer = document.getElementById('image-container');
  //Add title to image
  const location = document.getElementById('location-name')  
  location.innerText = imageName;
  const titleContainer = document.getElementById('location-title');
  // Remove the previous image.
  imageContainer.innerHTML = '';
  imageContainer.appendChild(imgElement);
  titleContainer.style.display = "inline-block";
}

function createImg(imageName) {
  const imgUrl = '/images/' + imageName + '.jpg';
  const imgElement = document.createElement('img');
  imgElement.src = imgUrl;
  imgElement.alt = 'I am sorry, the image cannot be displayed';
  imgElement.width = 300;
  imgElement.height = 300;
  return imgElement;
}

/**
 * Fetches the new comment and builds the UI.
 */
function getComments() {
  var maxComments = document.getElementById("maxComments").value;
  var fetchUrl = '/data?maxComments=' + maxComments;
  fetch(fetchUrl).then(response => response.json()).then((comments) => {
    // Build the list of history comments.
    const historyEl = document.getElementById('history');
    comments.forEach((comment) => {
      historyEl.appendChild(createCommentElement(comment));
    });
  });
}

/** Creates an <li> element containing text. */
function createCommentElement(text) {
  const commentElement = document.createElement('li');
  commentElement.className = 'comment';
  const textElement = document.createElement('span');
  textElement.innerText = text;
  commentElement.appendChild(textElement);
  return commentElement;
}

/** Changes number of comments displayed. */
function changeCommentsNum() {
  clearComments();
  getComments();
}

/** Deletes all comments. */
function deleteComments() {
  fetch('/delete-data', {method: 'POST'}).then(() => clearComments());
}

/** Clears out the displayed comments. */
function clearComments() {
    document.getElementById("history").innerHTML = "";
}