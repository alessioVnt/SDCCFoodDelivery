import pickle


# Utility function to get the trained model so far
def load_model(path):
    loaded_model = pickle.load(open(path, 'rb'))
    return loaded_model
