from nearestNeighbors.LoadModel import load_model
from sklearn.neighbors import NearestNeighbors


# Model class
# Navigate the model to get its closed distances
# Learn from streamm feature not used yet
class Streaming_learning:
    # MODEL_PATH it's an internal resources
    MODEL_PATH = './model/finalized_model.sav'
    knn_recomm = NearestNeighbors(metric='cosine', algorithm='brute')
    n_neighbors = 0

    def __init__(self, n_neighbors):
        self.n_neighbors = n_neighbors
        self.knn_recomm = load_model(self.MODEL_PATH)

    # Get the nearest interest for a given one
    # Explore the graph and return best choices according to the classification algorithm
    def get_nearest(self, X):
        return self.knn_recomm.kneighbors(X, n_neighbors=self.n_neighbors)

    # Fit one value into the model
    def fit_one(self, x):
        self.knn_recomm.fit(x)



